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
    $SQL_query = "UPDATE goals SET title = $1, description = $2, due_date = $3 WHERE id = $4 AND user_uuid = $5;";
    $params = array($title, $descrip, $due_date, (int)$goal_id, $uuid);
} elseif ($mode === "add") {
    $SQL_query = "INSERT INTO goals (description, title, due_date, user_uuid, completed) VALUES ($1, $2, $3, $4, false) RETURNING id;";
    $params = array($descrip, $title, $due_date, $uuid);
} else {
    pg_query_params($dbconn, "DELETE FROM goal_categories WHERE goal_id = $1", array((int)$goal_id));
    $SQL_query = "DELETE FROM goals WHERE id = $1 AND user_uuid = $2;";
    $params = array((int)$goal_id, $uuid);
}

$result = pg_query_params($dbconn, $SQL_query, $params);

if (!$result) {
    $error = pg_last_error($dbconn);
    echo json_encode(["status" => "error", "message" => $error]);
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
echo json_encode(["status" => "success", "message" => "change successful"]);
