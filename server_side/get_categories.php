<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");

if (!$db) {
    echo json_encode(array("status" => "error", "message" => "db fail"));
    exit;
}

if (!$uuid) {
    echo json_encode(array("status" => "error", "message" => "no uuid"));
    exit;
}

$sql = "select id, name from categories where user_uuid = $1 order by name asc";
$res = pg_query_params($db, $sql, array($uuid));

$cats = array();
while ($row = pg_fetch_assoc($res)) {
    $cats[] = $row;
}

pg_close($db);

header('Content-Type: application/json');
echo json_encode(array(
    "status" => "success",
    "categories" => $cats
));
?>
