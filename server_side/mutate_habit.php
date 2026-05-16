<?php
require_once __DIR__ . '/category_helpers.php';

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";

$uuid = $_POST['uuid'] ?? null;
$name = $_POST['name'] ?? null;
$desc = $_POST['description'] ?? '';
$mode = $_POST['mode'] ?? null;
$hid = $_POST['habit_id'] ?? null;
$cats = parse_category_ids($_POST['category_ids'] ?? '');

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));

if ($mode === "add") {
    $sql = "insert into habits (name, description, date_started, user_uuid) values ($1, $2, current_date, $3) returning id;";
    $p = array($name, $desc, $uuid);
} elseif ($mode === "edit") {
    $sql = "update habits set name = $1, description = $2 where id = $3 and user_uuid = $4;";
    $p = array($name, $desc, (int)$hid, $uuid);
} elseif ($mode === "delete") {
    $sql = "delete from habits where id = $1 and user_uuid = $2;";
    $p = array((int)$hid, $uuid);
} else {
    exit(json_encode(array("status" => "error")));
}

$res = pg_query_params($db, $sql, $p);
if (!$res) exit(json_encode(array("status" => "error")));

if ($mode === "add") {
    $row = pg_fetch_assoc($res);
    $hid = $row['id'];
}

if ($mode === "add" || $mode === "edit") {
    sync_habit_categories($db, $hid, $cats);
}

pg_close($db);
echo json_encode(array("status" => "success"));
?>
