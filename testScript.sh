requestBody="{\"email\":\"billsun26@gmail.com\",\"password\":\"testtest\"}"

curl -i \
-H "Accept: application/json" \
-H "Content-Type:application/json" \
-X POST --data "${requestBody}" \
http://localhost:9000/accounts/create
