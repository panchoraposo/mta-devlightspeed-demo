package com.acme.fsi.trading.api;

import com.acme.fsi.trading.model.Trade;
import com.acme.fsi.trading.service.TradeQueryDao;
import com.acme.fsi.trading.service.TradeService;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

@Path("/trades")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TradesResource {
  @EJB
  TradeService service;

  @EJB
  TradeQueryDao queries;

  @GET
  public List<Trade> list() {
    return service.list();
  }

  @GET
  @Path("/search")
  public List<Trade> search(@QueryParam("symbol") String symbol) {
    return queries.searchBySymbolUnsafe(symbol);
  }

  @POST
  public Response book(BookTradeRequest req) {
    if (req == null || req.getSymbol() == null || req.getSymbol().trim().isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\":\"symbol is required\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    BigDecimal notional = req.getNotional() == null ? new BigDecimal("100000.00") : req.getNotional();
    Trade t = service.book(req.getSymbol(), notional);
    return Response.status(Response.Status.CREATED).entity(t).build();
  }
}

