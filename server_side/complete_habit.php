<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";

$hid = $_POST['habit_id'] ?? null;
$uuid = $_POST['uuid'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));

if (!$hid || !$uuid) exit(json_encode(array("status" => "error")));

$sql1 = "select 1 from habit_completions where habit_id = $1 and completion_date = current_date";
$res1 = pg_query_params($db, $sql1, array($hid));

if ($res1 && pg_num_rows($res1) > 0) {
    echo json_encode(array("status" => "already_completed_today"));
    exit;
}

$sql2 = "insert into habit_completions (habit_id, user_uuid, completion_date) values ($1, $2, current_date)";
$res2 = pg_query_params($db, $sql2, array($hid, $uuid));

if ($res2) echo json_encode(array("status" => "success"));
else echo json_encode(array("status" => "error"));

pg_close($db);
?>
