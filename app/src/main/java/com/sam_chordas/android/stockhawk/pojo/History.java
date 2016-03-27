package com.sam_chordas.android.stockhawk.pojo;

import java.util.List;

public class History {

    /**
     * count : 4
     * created : 2016-03-20T05:01:19Z
     * lang : en-US
     * diagnostics : {"url":[{"execution-start-time":"1","execution-stop-time":"2","execution-time":"1","content":"http://www.datatables.org/yahoo/finance/yahoo.finance.historicaldata.xml"},{"execution-start-time":"7","execution-stop-time":"23","execution-time":"16","content":"http://ichart.finance.yahoo.com/table.csv?a=2&b=15&e=19&g=d&c=2016&d=2&f=2016&s=YHOO"},{"execution-start-time":"27","execution-stop-time":"28","execution-time":"1","content":"http://ichart.finance.yahoo.com/table.csv?a=2&b=15&e=19&g=d&c=2016&d=2&f=2016&s=YHOO"}],"publiclyCallable":"true","cache":[{"execution-start-time":"6","execution-stop-time":"6","execution-time":"0","method":"GET","type":"MEMCACHED","content":"f4c5d0598a8ce5bd68ddd132e3244d12"},{"execution-start-time":"25","execution-stop-time":"26","execution-time":"1","method":"GET","type":"MEMCACHED","content":"7c43816568e5202ed4177321bdc8f94e"}],"query":[{"execution-start-time":"7","execution-stop-time":"25","execution-time":"18","params":"{url=[http://ichart.finance.yahoo.com/table.csv?a=2&b=15&e=19&g=d&c=2016&d=2&f=2016&s=YHOO]}","content":"select * from csv(0,1) where url=@url"},{"execution-start-time":"26","execution-stop-time":"28","execution-time":"2","params":"{columnsNames=[Date,Open,High,Low,Close,Volume,Adj_Close], url=[http://ichart.finance.yahoo.com/table.csv?a=2&b=15&e=19&g=d&c=2016&d=2&f=2016&s=YHOO]}","content":"select * from csv(2,0) where url=@url and columns=@columnsNames"}],"javascript":{"execution-start-time":"4","execution-stop-time":"29","execution-time":"25","instructions-used":"41709","table-name":"yahoo.finance.historicaldata"},"user-time":"30","service-time":"19","build-version":"0.2.424"}
     * results : {"quote":[{"Symbol":"YHOO","Date":"2016-03-18","Open":"34.540001","High":"35.209999","Low":"34.380001","Close":"35.169998","Volume":"20490800","Adj_Close":"35.169998"},{"Symbol":"YHOO","Date":"2016-03-17","Open":"33.880001","High":"34.549999","Low":"33.869999","Close":"34.279999","Volume":"9334100","Adj_Close":"34.279999"},{"Symbol":"YHOO","Date":"2016-03-16","Open":"33.029999","High":"34.080002","Low":"33.00","Close":"34.009998","Volume":"10975700","Adj_Close":"34.009998"},{"Symbol":"YHOO","Date":"2016-03-15","Open":"33.32","High":"33.459999","Low":"33.110001","Close":"33.259998","Volume":"10660800","Adj_Close":"33.259998"}]}
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
        private String created;
        private String lang;
        private ResultsEntity results;

        public void setCount(int count) {
            this.count = count;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public void setResults(ResultsEntity results) {
            this.results = results;
        }

        public int getCount() {
            return count;
        }

        public String getCreated() {
            return created;
        }

        public String getLang() {
            return lang;
        }

        public ResultsEntity getResults() {
            return results;
        }

        public static class ResultsEntity {
            /**
             * Symbol : YHOO
             * Date : 2016-03-18
             * Open : 34.540001
             * High : 35.209999
             * Low : 34.380001
             * Close : 35.169998
             * Volume : 20490800
             * Adj_Close : 35.169998
             */

            private List<QuoteEntity> quote;

            public void setQuote(List<QuoteEntity> quote) {
                this.quote = quote;
            }

            public List<QuoteEntity> getQuote() {
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
