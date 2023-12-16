package pl.rationalworks.exchangeratetest.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;

import java.time.LocalDate;
import pl.rationalworks.exchangeratetest.model.Currency;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRatesRepository extends CrudRepository<ExchangeRate, ExchangeRateId> {

    Optional<ExchangeRate> findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate date, Currency targetCurrency);

    Optional<ExchangeRate> findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(Currency targetCurrency);

    Optional<List<ExchangeRate>> findAllByExchangeRateId_Date(LocalDate date);

    @Modifying
    @Query("update ExchangeRate r set r.requestCounter = r.requestCounter + 1 where r.exchangeRateId = :id")
    void updateRequestCounter(@Param("id") ExchangeRateId rateId);
}
