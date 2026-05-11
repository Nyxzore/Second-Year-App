<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
 echo "Unable to open database \n";
 exit;
} 

$SQL_query = "select * from goals where user_uuid = $1 and completed = false order by due_date " ;
$params = array($uuid);
$result = pg_query_params($dbconn, $SQL_query, $params);
if (!$result) {
 echo "Error with sql query: ".pg_last_error();
 exit;
}

$goals = [];
while ($row = pg_fetch_assoc($result)) {
    $goals[] = $row;
}

pg_close($dbconn);

header('Content-Type: application/json');
echo json_encode([
    "status" => "success",
    "goals" => $goals
]);
?>