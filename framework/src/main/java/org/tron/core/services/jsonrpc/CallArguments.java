package org.tron.core.services.jsonrpc;

import static org.tron.core.services.jsonrpc.JsonRpcApiUtil.addressHashToByteArray;
import static org.tron.core.services.jsonrpc.JsonRpcApiUtil.paramStringIsNull;

import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.core.Wallet;
import org.tron.core.exception.JsonRpcInvalidParamsException;
import org.tron.core.exception.JsonRpcInvalidRequestException;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CallArguments {

  public String from;
  public String to;
  public String gas = ""; //not used
  public String gasPrice = ""; //not used
  public String value = ""; //not used
  public String data;
  public String nonce;

  /**
   * just support TransferContract and TriggerSmartContract
   * */
  public ContractType getContractType(Wallet wallet) throws JsonRpcInvalidRequestException,
      JsonRpcInvalidParamsException {
    ContractType contractType;

    // from or to is null
    if (paramStringIsNull(from) || paramStringIsNull(to)) {
      throw new JsonRpcInvalidRequestException("invalid json request");
    } else {
      byte[] contractAddressData = addressHashToByteArray(to);
      BytesMessage.Builder build = BytesMessage.newBuilder();
      BytesMessage bytesMessage =
          build.setValue(ByteString.copyFrom(contractAddressData)).build();
      SmartContract smartContract = wallet.getContract(bytesMessage);

      // check if to is smart contract
      if (smartContract != null) {
        contractType = ContractType.TriggerSmartContract;
      } else {
        if (StringUtils.isNotEmpty(value)) {
          contractType = ContractType.TransferContract;
        } else {
          throw new JsonRpcInvalidRequestException("invalid json request");
        }
      }
    }
    return contractType;
  }
}