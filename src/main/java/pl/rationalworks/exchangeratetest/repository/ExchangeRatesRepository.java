package pl.rationalworks.exchangeratetest.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.exchangeratetest.model.Currency;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * In this repository class we are using two select statements and one update statement where we are using only one "id"
 * in the where clause.
 * Those methods are: <br/>
 * <ul>
 *     <li>{@link #findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate, Currency)}</li>
 *     <li>{@link #findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(Currency)}</li>
 *     <li>{@link #updateRequestCounter(ExchangeRateId)}</li>
 * </ul>
 * <br/>
 * If connecting to the database twice would be a bottleneck then we could change those methods and us "in" operator in the
 * {@code WHERE} clause.<br/>
 * This change would require following changes:
 * <ul>
 *     <li>methods which return {@code Optional<ExchangeRate>} should return a map {@code Map<Currency, ExchangeRate>}</li>
 *     <li>request counter update method should use {@code IN} operator in the {@code WHERE} clause, so we need to pass in the {@code List<ExchangeRateId>}</li>
 * </ul>
 * Here you can read more on how to return maps from the select statements: <br/>
 * <ul>
 *     <li>{@linkplain https://vladmihalcea.com/jpa-query-map-result/}</li>
 *     <li>{@linkplain https://vladmihalcea.com/jpa-sqlresultsetmapping/}</li>
 * </ul>
 *
 */
@Repository
public interface ExchangeRatesRepository extends CrudRepository<ExchangeRate, ExchangeRateId> {

    Optional<ExchangeRate> findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate date, Currency targetCurrency);

    Optional<ExchangeRate> findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(Currency targetCurrency);

    List<ExchangeRate> findAllByExchangeRateId_Date(LocalDate date);

    @Modifying
    @Query("update ExchangeRate r set r.requestCounter = r.requestCounter + 1 where r.exchangeRateId = :id")
    void updateRequestCounter(@Param("id") ExchangeRateId rateId);
}
