<?php
header('Content-Type: application/json');

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";

$habit_id = $_POST['habit_id'] ?? null;
$uuid = $_POST['uuid'] ?? null;

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Unable to open database"]);
    exit;
}
if (empty($habit_id) || empty($uuid)) {
    echo json_encode(["status" => "error", "message" => "Missing habit_id or uuid"]);
    exit;
}

$check = pg_query_params(
    $dbconn,
    "SELECT completion_date FROM habits WHERE id = $1 AND user_uuid = $2",
    array((int)$habit_id, $uuid)
);
if (!$check || pg_num_rows($check) === 0) {
    echo json_encode(["status" => "error", "message" => "Habit not found"]);
    exit;
}
$row = pg_fetch_assoc($check);
$today = date('Y-m-d');
if ($row['completion_date'] !== null && substr($row['completion_date'], 0, 10) === $today) {
    echo json_encode(["status" => "already_completed_today", "message" => "Already completed today"]);
    exit;
}

$upd = pg_query_params(
    $dbconn,
    "UPDATE habits SET completion_date = CURRENT_DATE WHERE id = $1 AND user_uuid = $2
     AND (completion_date IS NULL OR completion_date < CURRENT_DATE)",
    array((int)$habit_id, $uuid)
);

if (!$upd) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
    exit;
}
if (pg_affected_rows($dbconn) === 0) {
    echo json_encode(["status" => "already_completed_today", "message" => "Already completed today"]);
    exit;
}

echo json_encode(["status" => "success", "message" => "Completed for today"]);
pg_close($dbconn);