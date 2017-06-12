var missIntensity = db.getSiblingDb("missIntensity");

printjson(sh.shardCollection("missIntensity.accounts", {}, {unique: true}));
