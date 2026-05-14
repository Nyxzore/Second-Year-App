<?php
header('Content-Type: application/json');

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";

$habit_id = $_POST['habit_id'] ?? null;
$uuid = $_POST['uuid'] ?? null; // user_uuid

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

if (empty($habit_id) || empty($uuid)) {
    echo json_encode(["status" => "error", "message" => "Missing habit_id or uuid"]);
    exit;
}

// 1. Check if a completion already exists for today 
$check_sql = "SELECT 1 FROM habit_completions WHERE habit_id = $1 AND completion_date = CURRENT_DATE";
$check_res = pg_query_params($dbconn, $check_sql, array($habit_id));

if ($check_res && pg_num_rows($check_res) > 0) {
    echo json_encode(["status" => "already_completed_today", "message" => "Already completed today"]);
    exit;
}

// 2. Insert the new completion 
$insert_sql = "INSERT INTO habit_completions (habit_id, user_uuid, completion_date) VALUES ($1, $2, CURRENT_DATE)";
$insert_res = pg_query_params($dbconn, $insert_sql, array($habit_id, $uuid));

if ($insert_res) {
    echo json_encode(["status" => "success", "message" => "Completed for today"]);
} else {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
}

pg_close($dbconn);
?>