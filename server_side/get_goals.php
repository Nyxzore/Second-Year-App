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
        SELECT DISTINCT g.*
        FROM goals g
        INNER JOIN goal_categories gc ON gc.goal_id = g.id
        WHERE g.user_uuid = $1
          AND g.completed = false
          AND gc.category_id = $2
        ORDER BY g.due_date
    ";
    $params = array($uuid, (int)$category_id);
} else {
    $SQL_query = "SELECT * FROM goals WHERE user_uuid = $1 AND completed = false ORDER BY due_date";
    $params = array($uuid);
}

$result = pg_query_params($dbconn, $SQL_query, $params);
if (!$result) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
    exit;
}

$goals = [];
$goal_ids = [];
while ($row = pg_fetch_assoc($result)) {
    $goals[] = $row;
    $goal_ids[] = $row['id'];
}

$category_map = fetch_categories_for_goals($dbconn, $goal_ids);
foreach ($goals as &$goal) {
    $gid = (string)$goal['id'];
    $goal['categories'] = isset($category_map[$gid]) ? $category_map[$gid] : [];
}
unset($goal);

pg_close($dbconn);

echo json_encode([
    "status" => "success",
    "goals" => $goals
]);
