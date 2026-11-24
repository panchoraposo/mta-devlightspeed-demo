package com.acme.fsi.cards.api;

import com.acme.fsi.cards.jms.FraudQueueProducer;
import com.acme.fsi.cards.model.AuthEntity;
import com.acme.fsi.cards.service.AuthLedgerService;
import com.acme.fsi.cards.service.AuthStatusStore;
import com.acme.fsi.cards.service.LegacyJndiCardsLookup;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
@Path("/auths")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CardsResource {

  @EJB
  FraudQueueProducer producer;

  @EJB
  AuthStatusStore store;

  @EJB
  AuthLedgerService ledger;

  @GET
  public Response list(@QueryParam("limit") Integer limit) {
    int l = (limit == null) ? 10 : limit.intValue();
    List<AuthEntity> rows = ledger.listLatest(l);
    List<String> json = new ArrayList<String>(rows.size());
    for (AuthEntity e : rows) {
      json.add("{"
          + "\"authId\":\"" + e.getAuthId() + "\","
          + "\"merchantId\":\"" + e.getMerchantId() + "\","
          + "\"amount\":\"" + e.getAmount() + "\","
          + "\"currency\":\"" + e.getCurrency() + "\","
          + "\"flaggedFlag\":\"" + (Boolean.TRUE.equals(e.getFlaggedFlag()) ? "Y" : "N") + "\""
          + "}");
    }
    return Response.ok("[" + String.join(",", json) + "]").type(MediaType.APPLICATION_JSON).build();
  }

  @POST
  public Response authorize(AuthRequest req) {
    if (req == null || isBlank(req.getPan()) || isBlank(req.getMerchantId())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\":\"pan and merchantId are required\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    BigDecimal amount = req.getAmount() == null ? new BigDecimal("25.00") : req.getAmount();
    String currency = isBlank(req.getCurrency()) ? "USD" : req.getCurrency().trim();
    String authId = UUID.randomUUID().toString();
    String panLast4 = req.getPan().length() >= 4
        ? req.getPan().substring(req.getPan().length() - 4)
        : req.getPan();

    String payload = "{\"authId\":\"" + authId + "\",\"amount\":\"" + amount
        + "\",\"currency\":\"" + currency + "\",\"merchantId\":\"" + req.getMerchantId() + "\"}";
    store.setStatus(authId, "ENQUEUED");
    producer.send(payload);

    AuthEntity e = new AuthEntity();
    e.setMerchantId(req.getMerchantId().trim());
    e.setPanLast4(panLast4);
    e.setAmount(amount);
    e.setCurrency(currency);
    e.setFlaggedFlag(Boolean.FALSE);
    ledger.record(authId, e);

    // JNDI lookup legacy (intencional)
    new LegacyJndiCardsLookup().lookupCardsDs();

    return Response.status(Response.Status.ACCEPTED)
        .entity("{\"authId\":\"" + authId + "\",\"status\":\"ENQUEUED\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  @GET
  @Path("/{authId}")
  public Response status(@PathParam("authId") String authId) {
    String status = store.getStatus(authId);
    if (status == null) {
      List<AuthEntity> rows = ledger.findByAuthIdLegacyCriteria(authId);
      if (rows != null && !rows.isEmpty()) {
        AuthEntity e = rows.get(0);
        String derived = Boolean.TRUE.equals(e.getFlaggedFlag()) ? "FLAGGED" : "RECORDED";
        return Response.ok("{\"authId\":\"" + authId + "\",\"status\":\"" + derived + "\"}")
            .type(MediaType.APPLICATION_JSON)
            .build();
      }
      return Response.status(Response.Status.NOT_FOUND)
          .entity("{\"authId\":\"" + authId + "\",\"status\":\"NOT_FOUND\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    return Response.ok("{\"authId\":\"" + authId + "\",\"status\":\"" + status + "\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
