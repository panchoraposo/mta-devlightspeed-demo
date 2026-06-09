package com.acme.fsi.payments.api;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.acme.fsi.payments.jms.PaymentQueueProducer;
import com.acme.fsi.payments.model.PaymentEntity;
import com.acme.fsi.payments.service.LegacyJndiPaymentLookup;
import com.acme.fsi.payments.service.PaymentLedgerService;
import com.acme.fsi.payments.service.PaymentStatusStore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/payments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentsResource {

  @EJB
  PaymentQueueProducer producer;

  @EJB
  PaymentStatusStore store;

  @EJB
  PaymentLedgerService ledger;

  @GET
  public Response list(@QueryParam("limit") Integer limit) {
    int l = (limit == null) ? 10 : limit.intValue();
    List<PaymentEntity> rows = ledger.listLatest(l);
    List<String> json = new ArrayList<String>(rows.size());
    for (PaymentEntity e : rows) {
      json.add("{"
          + "\"paymentId\":\"" + e.getPaymentId() + "\","
          + "\"amount\":\"" + e.getAmount() + "\","
          + "\"currency\":\"" + e.getCurrency() + "\","
          + "\"createdAt\":\"" + e.getCreatedAt() + "\","
          + "\"clearedFlag\":\"" + (Boolean.TRUE.equals(e.getClearedFlag()) ? "Y" : "N") + "\""
          + "}");
    }
    return Response.ok("[" + String.join(",", json) + "]").type(MediaType.APPLICATION_JSON).build();
  }

  @POST
  public Response submit(PaymentRequest req) {
    if (req == null || isBlank(req.getDebtorIban()) || isBlank(req.getCreditorIban())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\":\"debtorIban and creditorIban are required\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    BigDecimal amount = req.getAmount() == null ? new BigDecimal("10.00") : req.getAmount();
    String currency = isBlank(req.getCurrency()) ? "USD" : req.getCurrency().trim();

    String paymentId = UUID.randomUUID().toString();
    String payload = "{\"paymentId\":\"" + paymentId + "\",\"amount\":\"" + amount + "\",\"currency\":\"" + currency + "\"}";
    store.setStatus(paymentId, "ENQUEUED");
    producer.send(payload);

    PaymentEntity e = new PaymentEntity();
    e.setAmount(amount);
    e.setCurrency(currency);
    ledger.record(paymentId, e);

    // JNDI lookup legacy (intencional): para disparar hallazgos al ir a Quarkus
    new LegacyJndiPaymentLookup().lookupPaymentsDs();

    return Response.status(Response.Status.ACCEPTED)
        .entity("{\"paymentId\":\"" + paymentId + "\",\"status\":\"ENQUEUED\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  @GET
  @Path("/{paymentId}")
  public Response status(@PathParam("paymentId") String paymentId) {
    String status = store.getStatus(paymentId);
    if (status == null) {
      // Si el pago fue "seeded" (persistido por JPA) no existe en el store en memoria:
      // fallback a la DB para poder consultarlo desde la UI.
      if (!isBlank(paymentId)) {
        List<PaymentEntity> rows = ledger.findByPaymentIdLegacyCriteria(paymentId);
        if (rows != null && !rows.isEmpty()) {
          PaymentEntity e = rows.get(0);
          String derived = Boolean.TRUE.equals(e.getClearedFlag()) ? "CLEARED" : "RECORDED";
          return Response.ok("{\"paymentId\":\"" + paymentId + "\",\"status\":\"" + derived + "\"}")
              .type(MediaType.APPLICATION_JSON)
              .build();
        }
      }

      return Response.status(Response.Status.NOT_FOUND)
          .entity("{\"paymentId\":\"" + paymentId + "\",\"status\":\"NOT_FOUND\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    return Response.ok("{\"paymentId\":\"" + paymentId + "\",\"status\":\"" + status + "\"}").type(MediaType.APPLICATION_JSON).build();
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}