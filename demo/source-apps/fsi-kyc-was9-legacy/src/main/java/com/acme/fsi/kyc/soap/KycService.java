package com.acme.fsi.kyc.soap;

import com.acme.fsi.kyc.model.KycApplication;
import com.acme.fsi.kyc.model.KycDecision;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(name = "KycService", targetNamespace = "http://fsi.acme.com/kyc")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public interface KycService {

  @WebMethod(operationName = "submitApplication")
  @WebResult(name = "decision")
  KycDecision submitApplication(@WebParam(name = "application") KycApplication application);

  @WebMethod(operationName = "getStatus")
  @WebResult(name = "decision")
  KycDecision getStatus(@WebParam(name = "applicationId") String applicationId);
}
