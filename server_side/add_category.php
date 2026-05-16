<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$name = trim($_POST['category_name'] ?? '');

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));

if (!$uuid || $name == "") {
    exit(json_encode(array("status" => "error", "message" => "missing info")));
}

$sql = "insert into categories (name, user_uuid) values ($1, $2) returning id, name";
$res = pg_query_params($db, $sql, array($name, $uuid));

if (!$res) {
    exit(json_encode(array("status" => "error", "message" => "exists or error")));
}

$row = pg_fetch_assoc($res);
pg_close($db);

header('Content-Type: application/json');
echo json_encode(array(
    "status" => "success",
    "category" => $row
));
?>
