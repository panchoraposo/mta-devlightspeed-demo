package com.acme.fsi.accounts.api;

import com.acme.fsi.accounts.model.AccountEntity;
import com.acme.fsi.accounts.service.AccountLedgerService;
import com.acme.fsi.accounts.service.LegacyJndiAccountsLookup;

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
@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountsResource {

  @EJB
  AccountLedgerService ledger;

  @GET
  public Response list(@QueryParam("limit") Integer limit) {
    int l = (limit == null) ? 10 : limit.intValue();
    List<AccountEntity> rows = ledger.listLatest(l);
    List<String> json = new ArrayList<String>(rows.size());
    for (AccountEntity e : rows) {
      json.add("{"
          + "\"accountId\":\"" + e.getAccountId() + "\","
          + "\"customerId\":\"" + e.getCustomerId() + "\","
          + "\"accountType\":\"" + e.getAccountType() + "\","
          + "\"balance\":\"" + e.getBalance() + "\","
          + "\"currency\":\"" + e.getCurrency() + "\","
          + "\"activeFlag\":\"" + (Boolean.TRUE.equals(e.getActiveFlag()) ? "Y" : "N") + "\""
          + "}");
    }
    return Response.ok("[" + String.join(",", json) + "]").type(MediaType.APPLICATION_JSON).build();
  }

  @POST
  public Response open(AccountRequest req) {
    if (req == null || isBlank(req.getCustomerId())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\":\"customerId is required\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }

    String accountId = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    AccountEntity e = new AccountEntity();
    e.setCustomerId(req.getCustomerId().trim());
    e.setAccountType(isBlank(req.getAccountType()) ? "CHECKING" : req.getAccountType().trim());
    e.setCurrency(isBlank(req.getCurrency()) ? "USD" : req.getCurrency().trim());
    e.setBalance(req.getOpeningBalance() == null ? BigDecimal.ZERO : req.getOpeningBalance());
    e.setActiveFlag(Boolean.TRUE);
    ledger.open(accountId, e);

    // JNDI lookup legacy (intencional): dispara hallazgos al ir a Quarkus
    new LegacyJndiAccountsLookup().lookupAccountsDs();

    return Response.status(Response.Status.CREATED)
        .entity("{\"accountId\":\"" + accountId + "\",\"status\":\"OPEN\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  @GET
  @Path("/{accountId}")
  public Response get(@PathParam("accountId") String accountId) {
    List<AccountEntity> rows = ledger.findByAccountIdLegacyCriteria(accountId);
    if (rows == null || rows.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity("{\"accountId\":\"" + accountId + "\",\"status\":\"NOT_FOUND\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    AccountEntity e = rows.get(0);
    String body = "{"
        + "\"accountId\":\"" + e.getAccountId() + "\","
        + "\"customerId\":\"" + e.getCustomerId() + "\","
        + "\"accountType\":\"" + e.getAccountType() + "\","
        + "\"balance\":\"" + e.getBalance() + "\","
        + "\"currency\":\"" + e.getCurrency() + "\","
        + "\"activeFlag\":\"" + (Boolean.TRUE.equals(e.getActiveFlag()) ? "Y" : "N") + "\""
        + "}";
    return Response.ok(body).type(MediaType.APPLICATION_JSON).build();
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
