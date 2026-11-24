package com.acme.fsi.accounts.api;

import com.acme.fsi.accounts.model.AccountEntity;
import com.acme.fsi.accounts.service.AccountLedgerService;
import com.acme.fsi.accounts.service.LegacyJndiAccountsLookup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountsResource {

  @Inject
  AccountLedgerService ledger;

  @Inject
  LegacyJndiAccountsLookup dsLookup;

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

    // Name kept for story continuity; now a CDI DataSource inject shim
    dsLookup.lookupAccountsDs();

    return Response.status(Response.Status.CREATED)
        .entity("{\"accountId\":\"" + accountId + "\",\"status\":\"OPEN\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  @GET
  @Path("/{accountId}")
  public Response get(@PathParam("accountId") String accountId) {
    List<AccountEntity> rows = ledger.findByAccountId(accountId);
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
