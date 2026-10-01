package com.travel.payment.mapper;

import com.travel.payment.entity.Payment;
import com.travel.payment.entity.PaymentTransaction;
import com.travel.payment.viewmodel.PaymentTransactionVm;
import com.travel.payment.viewmodel.PaymentVm;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct Mapper chuyển đổi dữ liệu qua lại giữa Entity và ViewModel
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentVm toPaymentVm(Payment payment);

    PaymentTransactionVm toPaymentTransactionVm(PaymentTransaction transaction);

    List<PaymentTransactionVm> toPaymentTransactionVmList(List<PaymentTransaction> transactions);
}
