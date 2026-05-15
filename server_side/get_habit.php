<?php
header('Content-Type: application/json');

require_once __DIR__ . '/category_helpers.php';

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$category_id = $_POST['category_id'] ?? '';

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

if ($category_id !== '' && ctype_digit((string)$category_id)) {
    $SQL_query = "
        SELECT DISTINCT h.id, h.user_uuid, h.name, h.description,
            EXISTS (
                SELECT 1
                FROM habit_completions hc
                WHERE hc.habit_id = h.id
                  AND hc.completion_date = CURRENT_DATE
            ) AS completed_today
        FROM habits h
        INNER JOIN habit_categories hc_cat ON hc_cat.habit_id = h.id
        WHERE h.user_uuid = $1
          AND hc_cat.category_id = $2
    ";
    $params = array($uuid, (int)$category_id);
} else {
    $SQL_query = "
        SELECT h.id, h.user_uuid, h.name, h.description,
            EXISTS (
                SELECT 1
                FROM habit_completions hc
                WHERE hc.habit_id = h.id
                  AND hc.completion_date = CURRENT_DATE
            ) AS completed_today
        FROM habits h
        WHERE h.user_uuid = $1
    ";
    $params = array($uuid);
}

$result = pg_query_params($dbconn, $SQL_query, $params);

if (!$result) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
    exit;
}

$habits = [];
$habit_ids = [];
while ($row = pg_fetch_assoc($result)) {
    $habits[] = $row;
    $habit_ids[] = $row['id'];
}

$category_map = fetch_categories_for_habits($dbconn, $habit_ids);
foreach ($habits as &$habit) {
    $hid = (string)$habit['id'];
    $habit['categories'] = isset($category_map[$hid]) ? $category_map[$hid] : [];
}
unset($habit);

pg_close($dbconn);

echo json_encode([
    "status" => "success",
    "habits" => $habits
]);
