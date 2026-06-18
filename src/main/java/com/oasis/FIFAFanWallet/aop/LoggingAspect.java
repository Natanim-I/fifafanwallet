package com.oasis.FIFAFanWallet.aop;

import com.oasis.FIFAFanWallet.dto.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger APP_LOGGER = LoggerFactory.getLogger("APP_LOGGER");
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ERROR_LOGGER");

    @AfterThrowing(value = "execution(* com.oasis.FIFAFanWallet.service..*(..))", throwing = "ex")
    public void logServiceCallErrors(JoinPoint jp, Throwable ex) {
        ERROR_LOGGER.error(
                "Error in {}.",
                jp.getSignature().getName(),
                ex
        );
    }

    @AfterReturning(value = "execution(* com.oasis.FIFAFanWallet.service..*(..))")
    public void logServiceCalls(JoinPoint jp) {
        APP_LOGGER.info(
                "UserService method {} executed successfully.",
                jp.getSignature().getName()
        );
    }

    @AfterReturning(value = "execution(* com.oasis.FIFAFanWallet.service.UserService.registerUser(..))", returning = "result")
    public void logUserRegistration(JoinPoint jp, UserResponse result){
        APP_LOGGER.info(
                "User registered -> User Id: {}.",
                result.userId()
        );
    }

    @AfterReturning(value = "execution(* com.oasis.FIFAFanWallet.service.WalletService.createUserWallet(..))", returning = "result")
    public void logUserWalletCreation(JoinPoint jp, WalletResponse result){
        APP_LOGGER.info(
                "User created wallet -> Wallet Id: {}.",
                result.walletId()
        );
    }

    @AfterReturning(value = "execution(* com.oasis.FIFAFanWallet.service.WalletService.disableUserWallet(..))")
    public void logUserDisableWallet(JoinPoint jp){
        APP_LOGGER.info(
                "User disabled wallet -> Wallet Id: {}.",
                jp.getArgs()[0]
        );
    }

    @AfterReturning(value = """
             execution(* com.oasis.FIFAFanWallet.service.TransactionService.deposit(..) || 
             execution(* com.oasis.FIFAFanWallet.service.TransactionService.withdraw(..)
             """, returning = "result")
    public void logDepositWithdrawCalls(JoinPoint jp, TransactionResponse result){
        APP_LOGGER.info("Transaction {} successfully completed. Transaction Id: {}.",
                jp.getSignature().getName(),
                result.id());
    }

    @AfterReturning(value = "execution(* com.oasis.FIFAFanWallet.service.TransactionService.transfer(..)", returning = "result")
    public void logTransferCalls(JoinPoint jp, TransferResponse result){
        APP_LOGGER.info("Transfer method ({}) successfully executed. Transaction ID: {}.",
                jp.getSignature().getName(),
                result.id());
    }

    @AfterReturning(value = "execution(* com.oasis.FIFAFanWallet.service.TransactionService.exchange(..)", returning = "result")
    public void logExchangeCalls(JoinPoint jp, ExchangeResponse result){
        APP_LOGGER.info("Exchange method ({}) successfully executed. Transaction ID: {}.",
                jp.getSignature().getName(),
                result.id());
    }

    @AfterReturning(value = "* com.oasis.FIFAFanWallet.service.BudgetService.createBudget(..)", returning = "result")
    public void logCreateBudgetCall(JoinPoint jp, BudgetResponse result){
        APP_LOGGER.info("Budget created, method {} executed successfully. Budget Id: {}.",
                jp.getSignature().getName(),
                result.budgetId());
    }

    @AfterReturning(value = "* com.oasis.FIFAFanWallet.service.BudgetService.updateBudget(..)", returning = "result")
    public void logUpdateBudgetCall(JoinPoint jp, BudgetResponse result){
        APP_LOGGER.info("Budget updated, method {} executed successfully. Budget Id: {}.",
                jp.getSignature().getName(),
                result.budgetId());
    }

    @AfterReturning(value = "* com.oasis.FIFAFanWallet.service.BudgetService.updateBudget(..)")
    public void logDeleteBudgetCall(JoinPoint jp){
        APP_LOGGER.info("Budget deleted, method {} executed successfully. Budget Id: {}.",
                jp.getSignature().getName(),
                jp.getArgs()[0]);
    }

    @AfterReturning(value = "* com.oasis.FIFAFanWallet.service.PaymentService.makePayment(..)", returning = "result")
    public void logMakePaymentCall(JoinPoint jp, PaymentResponse result){
        APP_LOGGER.info("Payment made, method {} executed successfully. Payment Id: {}.",
                jp.getSignature().getName(),
                result.paymentId());
    }
}
