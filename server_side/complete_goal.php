<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$goal_id = $_POST['goal_id'] ?? null;

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
 echo "Unable to open database \n";
 exit;
} 

$SQL_query = "UPDATE goals SET completed = true where id =$1;";
$params = array($goal_id);
$result = pg_query_params($dbconn, $SQL_query, $params);
if (!$result) {
 echo "Error with sql query: ".pg_last_error();
 exit;
}

pg_close($dbconn);

header('Content-Type: application/json');
echo json_encode([
    "status" => "success",
    "message" => "Congrats!"
]);
?>

