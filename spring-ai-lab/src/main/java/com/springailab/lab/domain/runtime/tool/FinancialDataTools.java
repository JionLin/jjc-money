package com.springailab.lab.domain.runtime.tool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 金融数据工具类。
 * 修复版：增加了 data-symbol 约束，防止抓取到错误的标的数据。
 */
@Configuration
public class FinancialDataTools {

    private static final Logger log = LoggerFactory.getLogger(FinancialDataTools.class);

    @Bean
    @Description("Get real-time stock price and valuation metrics for a given ticker (e.g., NVDA, AAPL)")
    public Function<StockRequest, StockResponse> getRealTimeStockData() {
        return request -> {
            String ticker = request.ticker().toUpperCase().trim();
            log.info("Fetching real-time data for ticker: {}", ticker);

            try {
                // 使用 Yahoo Finance 详情页
                String url = "https://finance.yahoo.com/quote/" + ticker;
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();

                Map<String, Object> facts = new HashMap<>();

                // 1. 提取价格 - 必须指定 data-symbol 避免抓到侧边栏的热门股票
                Element priceElement = doc.selectFirst("fin-streamer[data-field=regularMarketPrice][data-symbol=" + ticker + "]");
                if (priceElement != null) {
                    facts.put("price", priceElement.text());
                }

                // 2. 提取市值
                Element mcElement = doc.selectFirst("fin-streamer[data-field=marketCap][data-symbol=" + ticker + "]");
                if (mcElement != null) {
                    facts.put("market_cap", mcElement.text());
                }

                // 3. 提取 52 周波动范围
                Element rangeElement = doc.selectFirst("fin-streamer[data-field=fiftyTwoWeekRange][data-symbol=" + ticker + "]");
                if (rangeElement != null) {
                    facts.put("52_week_range", rangeElement.text());
                }

                // 4. 提取 PE (Forward) - 尝试从统计表格中获取
                Element peElement = doc.selectFirst("td[data-test=FORWARD_PE_VALUE] span");
                if (peElement == null) {
                    // 备选选择器：查找包含 PE 字样的表格行
                    Element peRow = doc.selectFirst("tr:contains(Forward P/E)");
                    if (peRow != null) {
                        peElement = peRow.selectFirst("td:nth-child(2)");
                    }
                }
                
                if (peElement != null) {
                    facts.put("valuation_pe_forward", peElement.text());
                }

                // 5. 提取 PE (Trailing)
                Element trailingPe = doc.selectFirst("td[data-test=PE_RATIO-value]");
                if (trailingPe != null) {
                    facts.put("valuation_pe_trailing", trailingPe.text());
                }

                if (facts.isEmpty()) {
                    return new StockResponse(ticker, false, "Could not extract data for " + ticker + ". HTML structure might have changed.", facts);
                }

                log.info("Successfully fetched data for {}: {}", ticker, facts);
                return new StockResponse(ticker, true, "Success from Yahoo Finance", facts);

            } catch (Exception e) {
                log.error("Failed to fetch data for {}: {}", ticker, e.getMessage());
                return new StockResponse(ticker, false, "Error: " + e.getMessage(), Map.of());
            }
        };
    }

    public record StockRequest(String ticker) {}
    public record StockResponse(String ticker, boolean success, String message, Map<String, Object> facts) {}
}
