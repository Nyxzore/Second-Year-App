<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";
$gid = $_POST['goal_id'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) die("db error");

$sql = "update goals set completed = true where id = $1;";
$res = pg_query_params($db, $sql, array($gid));

pg_close($db);
header('Content-Type: application/json');
echo json_encode(array("status" => "success"));
?>
