<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");

if (!$db) exit(json_encode(array("status" => "error")));

$res1 = pg_query_params($db, "select count(id) as count from goals where user_uuid = $1 and completed = true", array($uuid));
$comp = pg_fetch_assoc($res1)['count'] ?? 0;

$res2 = pg_query_params($db, "select count(id) as count from goals where user_uuid = $1 and completed = false", array($uuid));
$act = pg_fetch_assoc($res2)['count'] ?? 0;

pg_close($db);

header('Content-Type: application/json');
echo json_encode(array(
    "status" => "success",
    "completed_count" => (int)$comp,
    "active_count" => (int)$act
));
?>
