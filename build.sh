cd moderator-web;
bash build.sh $1;
cd ..;
gzip -k src/main/webapp/resources/moderator/vendor.bundle.js;
mvn package;
