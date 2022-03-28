FROM elasticsearch:7.9.3

RUN elasticsearch-plugin install --batch analysis-icu
RUN cd lib/ && zip -q -d log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class
