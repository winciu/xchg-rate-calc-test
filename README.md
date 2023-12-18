 # Exchange rate calculation service
 
This is a Spring boot service for evaluating exchange rate for two currencies, including a pre-defined currency spread.
Together with the exchange rate, each currency is associated with a spread percentage. The spread percentage
values are given in the table below.

| **Currency**    | **Spread percentage** |
|-----------------|-----------------------|
| Base currency   | 0%                    |
| JPY, HKD, KRW   | 3.25%                 |
| MYR, INR, MXN   | 4.50%                 |
| RUB, CNY, ZAR   | 6.00%                 |
| Other currencies | 2.75%                |

The exchange rate calculation needs to take into consideration the highest spread of the 2 currencies,
hence it should be calculated as follows:

\frac{toCurrencyExchangeRateToUSD}{fromCurrencyExchangeRateToUSD}\times\frac{100 - MAX(toCurrencySpread, fromCurrencySpread)}{100}

Let's take the following example:

|                          | **EUR** | **PLN** |
|--------------------------|---------|---------|
| **Exchnage Rate To USD** | 0.8     | 3.7     |
| **Spread**               | 1%      | 4%      |

The exchange rate to convert EURs into PLNs would be worked out as:

    \frac{3.7}{0.8}\times\frac{100 - 4}{100}=4.625\times0.96=4.44

```math
\frac{1}{@}
```

# Build

To build the service and run tests, you need to download the source code and save it in the folder of your choice.
Then go to that folder and run the following command to build the jar file.

    mvn clean install

As a prerequisite you need to have `Maven` and `Java 17` already installed.

# Run

Once the service is built, you can run it by executing the command below.

    java -jar target\exchange-rate-test-0.0.1-SNAPSHOT.jar [OPTIONAL_PARAMS]

# Usage
This service exposes two endpoints, namely:

    GET /exchange  //calculate exchnage rate by providing required params in the query string
    PUT /exchange  // manulaly fetch latest rates from Fixer

#### GET /exchange
This endpoint uses 3 params: currencyFrom (source), currencyTo (target), date. The `date` param is optional but should be in ISO8601 format.
The example request looks like this:

    curl "http://localhost:8082/exchange?from=USD&to=PLN"

If the date is not specified, the latest one is used. If you want to specify a date from which the exchange rates should be used, then use this one:

    curl "http://localhost:8082/exchange?from=USD&to=PLN&date=2023-12-15"

Once the request is successful, then you will get the response similar to the one below:

```
{
  "from": "USD",
  "to": "PLN",
  "exchange": 3.8648371093836001875
}
```

This endpoint uses currency spread while evaluating the final exchange rate. What spread should be used
for a given currency is set in the `application.yaml` file.

#### PUT /exchange

This is a convenience endpoint for fetching the latest rates from the Fixer provider. We can use it if 
we do not want to wait for the next scheduler execution to fetch the exchange rates.
There are no input parameters in this endpoint, so it is eanough to just use:

    curl -X PUT "http://localhost:8082/exchange"

In the successful response, you should get all the already fetched rates which were just saved in the database. A response
will look like the one below:

```
{
  "timestamp": 1702848243000,
  "base": "EUR",
  "date": "2023-12-17",
  "rates": {
    "AED": 4.003432,
    "AFN": 76.558366,
    "ALL": 103.916341,
    "AMD": 443.343084,
    ...
    "ZWL":351.002878        
  }
}
```

### Fixer API
This service is using Fixer API (https://fixer.io/) for fetching current exchange rates. It uses (`/latest`) endpoint, that is

    http://data.fixer.io/api/latest?access_key=ACESS_KEY

The default access key is defined in `application.yaml` file, but you can use your own. To do that, please specify the following
parameter when starting the service:

    --providers.fixer.api-key=YOUR_KEY

# Scheduling
This service has a scheduler implemented to fetch the latest rates automatically at a given time.
Scheduler uses cron expression to specify its start moment.
By default, the scheduler is set at 12:05 AM (GMT). You can configure both the cron expression and the timezone by specifying runtime parameters in the command line.

To run the scheduler every two minutes use `--service.scheduling.fixer.cron="0 0/2 * * * *"` parameter.
Alternatively, if you want to run it at a fixed time, you will need to set the correct time zone as well.
This could be done like mentioned below:

    --service.scheduling.fixer.timezone="Europe/Warsaw"
    --service.scheduling.fixer.timezone="GMT+1"


# Swagger / Open API

If you would like to check/test this service using its REST API via Swagger, then after running
the service, go to the web browser and type:

    http://localhost:8082/swagger-ui/index.html

# Database
This service is using the H2 relation database and stores the data (fetched exchange rates) in memory.
If you would like to use another DB then please specify all the required connection information in the 
`application.yaml` file or by using parameters in the command line during service startup.

---
Please notice that the default service port is `8082`. 
This value is also configurable in the `application.yaml` file or by specifying the corresponding runtime parameter (`--server.port`).