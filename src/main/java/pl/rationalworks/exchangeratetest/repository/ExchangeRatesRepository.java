package pl.rationalworks.exchangeratetest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

@Repository
public interface ExchangeRatesRepository extends CrudRepository<ExchangeRate, ExchangeRateId> {

    Optional<ExchangeRate> findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate date, Currency targetCurrency);

    Optional<ExchangeRate> findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(Currency targetCurrency);
}
