<?php
header('Content-Type: application/json');

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Unable to open database"]);
    exit;
}
if (empty($uuid)) {
    echo json_encode(["status" => "error", "message" => "Missing uuid"]);
    exit;
}

$SQL_query = "
SELECT h.id,h.user_uuid,h.name,h.description,
    EXISTS (
        SELECT 1
        FROM habit_completions hc
        WHERE hc.habit_id = h.id
          AND hc.completion_date = CURRENT_DATE
    ) AS completed_today
FROM habits h
WHERE h.user_uuid = $1;";


$result = pg_query_params($dbconn, $SQL_query, array($uuid));

if (!$result) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
    exit;
}

$habits = [];
while ($row = pg_fetch_assoc($result)) {
    $habits[] = $row;
}

pg_close($dbconn);

echo json_encode([
    "status" => "success",
    "habits" => $habits
]);