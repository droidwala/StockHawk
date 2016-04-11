package com.sam_chordas.android.stockhawk.pojo;

/**
 * Used to store values parsed in JSON(using GSON parsing) when result_count is 1 in response received.
 */
public class OneDayHistory {

    /**
     * count : 1
     * created : 2016-03-22T06:53:28Z
     * lang : en-US
     * diagnostics : {"url":[{"execution-start-time":"1","execution-stop-time":"2","execution-time":"1","content":"http://www.datatables.org/yahoo/finance/yahoo.finance.historicaldata.xml"},{"execution-start-time":"7","execution-stop-time":"26","execution-time":"19","content":"http://ichart.finance.yahoo.com/table.csv?a=2&b=21&e=25&g=d&c=2016&d=2&f=2016&s=YHOO"},{"execution-start-time":"30","execution-stop-time":"31","execution-time":"1","content":"http://ichart.finance.yahoo.com/table.csv?a=2&b=21&e=25&g=d&c=2016&d=2&f=2016&s=YHOO"}],"publiclyCallable":"true","cache":[{"execution-start-time":"5","execution-stop-time":"5","execution-time":"0","method":"GET","type":"MEMCACHED","content":"53c76d3d40ff2280b82e28f264ffe9f8"},{"execution-start-time":"29","execution-stop-time":"29","execution-time":"0","method":"GET","type":"MEMCACHED","content":"813c8a85ef209a9a2f49af1e80619800"}],"query":[{"execution-start-time":"6","execution-stop-time":"28","execution-time":"22","params":"{url=[http://ichart.finance.yahoo.com/table.csv?a=2&b=21&e=25&g=d&c=2016&d=2&f=2016&s=YHOO]}","content":"select * from csv(0,1) where url=@url"},{"execution-start-time":"29","execution-stop-time":"31","execution-time":"2","params":"{columnsNames=[Date,Open,High,Low,Close,Volume,Adj_Close], url=[http://ichart.finance.yahoo.com/table.csv?a=2&b=21&e=25&g=d&c=2016&d=2&f=2016&s=YHOO]}","content":"select * from csv(2,0) where url=@url and columns=@columnsNames"}],"javascript":{"execution-start-time":"3","execution-stop-time":"32","execution-time":"28","instructions-used":"32654","table-name":"yahoo.finance.historicaldata"},"user-time":"33","service-time":"21","build-version":"0.2.424"}
     * results : {"quote":{"Symbol":"YHOO","Date":"2016-03-21","Open":"35.00","High":"36.099998","Low":"35.00","Close":"35.470001","Volume":"13279400","Adj_Close":"35.470001"}}
     */

    private QueryEntity query;

    public void setQuery(QueryEntity query) {
        this.query = query;
    }

    public QueryEntity getQuery() {
        return query;
    }

    public static class QueryEntity {
        private int count;
        /**
         * quote : {"Symbol":"YHOO","Date":"2016-03-21","Open":"35.00","High":"36.099998","Low":"35.00","Close":"35.470001","Volume":"13279400","Adj_Close":"35.470001"}
         */

        private ResultsEntity results;

        public void setCount(int count) {
            this.count = count;
        }

        public void setResults(ResultsEntity results) {
            this.results = results;
        }

        public int getCount() {
            return count;
        }

        public ResultsEntity getResults() {
            return results;
        }

        public static class ResultsEntity {
            /**
             * Symbol : YHOO
             * Date : 2016-03-21
             * Open : 35.00
             * High : 36.099998
             * Low : 35.00
             * Close : 35.470001
             * Volume : 13279400
             * Adj_Close : 35.470001
             */

            private QuoteEntity quote;

            public void setQuote(QuoteEntity quote) {
                this.quote = quote;
            }

            public QuoteEntity getQuote() {
                return quote;
            }

            public static class QuoteEntity {
                private String Symbol;
                private String Date;
                private String Open;
                private String High;
                private String Low;
                private String Close;
                private String Volume;
                private String Adj_Close;

                public void setSymbol(String Symbol) {
                    this.Symbol = Symbol;
                }

                public void setDate(String Date) {
                    this.Date = Date;
                }

                public void setOpen(String Open) {
                    this.Open = Open;
                }

                public void setHigh(String High) {
                    this.High = High;
                }

                public void setLow(String Low) {
                    this.Low = Low;
                }

                public void setClose(String Close) {
                    this.Close = Close;
                }

                public void setVolume(String Volume) {
                    this.Volume = Volume;
                }

                public void setAdj_Close(String Adj_Close) {
                    this.Adj_Close = Adj_Close;
                }

                public String getSymbol() {
                    return Symbol;
                }

                public String getDate() {
                    return Date;
                }

                public String getOpen() {
                    return Open;
                }

                public String getHigh() {
                    return High;
                }

                public String getLow() {
                    return Low;
                }

                public String getClose() {
                    return Close;
                }

                public String getVolume() {
                    return Volume;
                }

                public String getAdj_Close() {
                    return Adj_Close;
                }
            }
        }
    }
}
