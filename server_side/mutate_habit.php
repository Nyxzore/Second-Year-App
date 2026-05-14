<?php
header('Content-Type: application/json');

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";

$uuid = $_POST['uuid'] ?? null;
$name = $_POST['name'] ?? null;
$description = $_POST['description'] ?? '';
$mode = $_POST['mode'] ?? null;
$habit_id = $_POST['habit_id'] ?? null;

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Unable to open database"]);
    exit;
}

if ($mode === "add") {
    if (empty($uuid) || empty($name)) {
        echo json_encode(["status" => "error", "message" => "Missing uuid or name"]);
        exit;
    }
    $SQL_query = "INSERT INTO habits (name, description, date_started, user_uuid)
                  VALUES ($1, $2, CURRENT_DATE, $3);";
    $params = array($name, $description, $uuid);
} elseif ($mode === "edit") {
    if (empty($habit_id) || empty($name)) {
        echo json_encode(["status" => "error", "message" => "Missing habit_id or name"]);
        exit;
    }
    $SQL_query = "UPDATE habits SET name = $1, description = $2 WHERE id = $3 AND user_uuid = $4;";
    $params = array($name, $description, (int)$habit_id, $uuid);
} elseif ($mode === "delete") {
    if (empty($habit_id)) {
        echo json_encode(["status" => "error", "message" => "Missing habit_id"]);
        exit;
    }
    $SQL_query = "DELETE FROM habits WHERE id = $1 AND user_uuid = $2;";
    $params = array((int)$habit_id, $uuid);
} else {
    echo json_encode(["status" => "error", "message" => "Invalid mode"]);
    exit;
}

$result = pg_query_params($dbconn, $SQL_query, $params);

if (!$result) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
} else {
    echo json_encode(["status" => "success", "message" => "change successful"]);
}

pg_close($dbconn);
