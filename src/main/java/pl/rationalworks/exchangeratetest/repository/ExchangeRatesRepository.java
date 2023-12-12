package pl.rationalworks.exchangeratetest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExchangeRatesRepository extends CrudRepository<ExchangeRate, ExchangeRateId> {

    List<ExchangeRate> findAllByDate(LocalDate date);
}
