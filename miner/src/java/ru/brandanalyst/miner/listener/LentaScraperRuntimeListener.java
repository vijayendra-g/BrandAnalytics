package ru.brandanalyst.miner.listener;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperRuntimeListener;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.variables.Variable;
import ru.brandanalyst.core.db.provider.ArticleProvider;
import ru.brandanalyst.core.model.Article;
import ru.brandanalyst.miner.util.LentaDataTransformator;

import java.sql.Timestamp;
import java.util.Map;

public class LentaScraperRuntimeListener implements ScraperRuntimeListener {
    private static final Logger log = Logger.getLogger(ScraperRuntimeListener.class);

       protected SimpleJdbcTemplate jdbcTemplate;
       protected ArticleProvider articleProvider;

       public LentaScraperRuntimeListener(SimpleJdbcTemplate jdbcTemplate) {
           this.jdbcTemplate = jdbcTemplate;
           articleProvider = new ArticleProvider(jdbcTemplate);
       }

       private Timestamp evalTimestamp(String stringDate) {
           int nano = 0;
           int second = 0;
           stringDate=stringDate.replace(" ","").replace(",","");
           int minute = Integer.parseInt(stringDate.substring(13,15));
           int hour = Integer.parseInt(stringDate.substring(10,12));
           int day = Integer.parseInt(stringDate.substring(0,2));
           int month = Integer.parseInt(stringDate.substring(3,5));
           int year = Integer.parseInt(stringDate.substring(6,10)) - 1900;
           Timestamp timestamp = new Timestamp(year,month-1,day,hour,minute,second,nano);
           return timestamp;
       }

       public void onExecutionStart(Scraper scraper) {
       }

       public void onExecutionPaused(Scraper scraper) {
       }

       public void onExecutionContinued(Scraper scraper) {
       }

       public void onNewProcessorExecution(Scraper scraper, BaseProcessor baseProcessor) {
       if("body".equalsIgnoreCase(baseProcessor.getElementDef().getShortElementName())) {
            String oneNew = scraper.getContext().get("oneNew").toString();
            if(oneNew.substring(15,21).equals("/news/")){
                scraper.addVariableToContext("kind",0);
            }
            else if (oneNew.substring(15,25).equals("/articles/")){
                scraper.addVariableToContext("kind",1);
            }
            else if (oneNew.substring(15,22).equals("/news2/")){
                scraper.addVariableToContext("kind",2);
            }
        }
       }

       public void onExecutionEnd(Scraper scraper) {

       }

       public void onProcessorExecutionFinished(Scraper scraper, BaseProcessor baseProcessor, Map map) {
           if("body".equalsIgnoreCase(baseProcessor.getElementDef().getShortElementName())){
               String articleLink = scraper.getContext().get("oneNew").toString();
               if (articleLink.contains("/lib/") || articleLink.contains("/photo/") || articleLink.contains("/online/") || articleLink.contains("/story/") || articleLink.contains("/conf/") || articleLink.contains("/columns/") || !articleLink.contains("http://lenta.ru/"))
               {
                    log.info("Lenta: Proceed...");
                    return;
               }
               Variable newsTitle = (Variable)scraper.getContext().get("newsTitle");
               Variable newsText = (Variable)scraper.getContext().get("newsFullText");
               Variable newsDate = (Variable)scraper.getContext().get("newsDate");
               if ((newsTitle.toString().isEmpty()) || (newsText.toString().isEmpty()) || (newsDate.toString().isEmpty()))
               {
                   log.info("Lenta: Invalid structure...");
                   return;
               }
               long brandId = ((Variable)scraper.getContext().get("brandId")).toLong();
               Timestamp articleTimestamp = evalTimestamp(newsDate.toString());
               String articleContent="";
               Integer kind=Integer.parseInt(scraper.getContext().get("kind").toString());
               if (kind==0) articleContent = LentaDataTransformator.clearNewsString(newsText.toString(), articleLink);
               else if (kind==1) articleContent = LentaDataTransformator.clearArticlesString(newsText.toString());
               else articleContent=LentaDataTransformator.clearString(newsText.toString());
               String articleTitle = newsTitle.toString();
               Article article = new Article(-1,brandId,3,articleTitle,articleContent,articleLink,articleTimestamp,0);
               articleProvider.writeArticleToDataStore(article);
               log.info("Lenta: article added...");
           }
           if("empty".equalsIgnoreCase(baseProcessor.getElementDef().getShortElementName())) {
        Integer countOfArticles=Integer.parseInt(scraper.getContext().get("countOfArticles").toString());
           int maxPage=countOfArticles/25;
           scraper.addVariableToContext("maxPage",maxPage);
            }
       }

       public void onExecutionError(Scraper scraper, Exception e) {
           if(e != null) {
           System.out.println(e.getCause().getMessage());
           }
       }
   }


