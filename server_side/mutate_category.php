<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";

$uuid = $_POST['uuid'] ?? null;
$name = trim($_POST['category_name'] ?? '');
$mode = $_POST['mode'] ?? 'add';
$cid = $_POST['category_id'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error", "message" => "DB connection failed")));

if (!$uuid) {
    exit(json_encode(array("status" => "error", "message" => "missing user uuid")));
}

if ($mode === "add") {
    if ($name == "") exit(json_encode(array("status" => "error", "message" => "name required")));
    $sql = "insert into categories (name, user_uuid) values ($1, $2) returning id, name";
    $res = pg_query_params($db, $sql, array($name, $uuid));
} elseif ($mode === "edit") {
    if (!$cid || $name == "") exit(json_encode(array("status" => "error", "message" => "id and name required")));
    $sql = "update categories set name = $1 where id = $2 and user_uuid = $3 returning id, name";
    $res = pg_query_params($db, $sql, array($name, (int)$cid, $uuid));
} elseif ($mode === "delete") {
    if (!$cid) exit(json_encode(array("status" => "error", "message" => "id required")));
    $sql = "delete from categories where id = $1 and user_uuid = $2";
    $res = pg_query_params($db, $sql, array((int)$cid, $uuid));
    if ($res) {
        pg_close($db);
        exit(json_encode(array("status" => "success", "message" => "deleted")));
    }
} else {
    exit(json_encode(array("status" => "error", "message" => "invalid mode")));
}

if (!$res) {
    exit(json_encode(array("status" => "error", "message" => pg_last_error($db))));
}

$row = pg_fetch_assoc($res);
pg_close($db);

header('Content-Type: application/json');
echo json_encode(array(
    "status" => "success",
    "category" => $row
));
?>
