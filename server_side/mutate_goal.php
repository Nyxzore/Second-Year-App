<?php
header('Content-Type: application/json');

require_once __DIR__ . '/category_helpers.php';

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$descrip = $_POST['description'] ?? null;
$title = $_POST['title'] ?? null;
$due_date = $_POST['due_date'] ?? null;
$mode = $_POST['mode'] ?? null;
$goal_id = $_POST['goal_id'] ?? null;
$category_ids = parse_category_ids($_POST['category_ids'] ?? '');

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Unable to open database"]);
    exit;
}

if ($mode === "edit") {
    $sql = "update goals set title = $1, description = $2, due_date = $3 where id = $4 and user_uuid = $5;";
    $params = array($title, $descrip, $due_date, (int)$goal_id, $uuid);
} elseif ($mode === "add") {
    $sql = "insert into goals (description, title, due_date, user_uuid, completed) values ($1, $2, $3, $4, false) returning id;";
    $params = array($descrip, $title, $due_date, $uuid);
} else {
    pg_query_params($dbconn, "delete from goal_categories where goal_id = $1", array((int)$goal_id));
    $sql = "delete from goals where id = $1 and user_uuid = $2;";
    $params = array((int)$goal_id, $uuid);
}

$result = pg_query_params($dbconn, $sql, $params);

if (!$result) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
    pg_close($dbconn);
    exit;
}

if ($mode === "add") {
    $row = pg_fetch_assoc($result);
    $goal_id = $row['id'];
}

if ($mode === "add" || $mode === "edit") {
    sync_goal_categories($dbconn, $goal_id, $category_ids, $uuid);
}

pg_close($dbconn);
echo json_encode(["status" => "success", "message" => "change succesful"]);
