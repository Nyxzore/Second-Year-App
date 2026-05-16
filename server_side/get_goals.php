<?php

require_once __DIR__ . '/category_helpers.php';

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$cid = $_POST['category_id'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");

if (!$db) exit(json_encode(array("status" => "error", "message" => "db error")));
if (!$uuid) exit(json_encode(array("status" => "error", "message" => "no uuid")));

if ($cid) {
    $sql = "select g.* from goals g join goal_categories gc on g.id = gc.goal_id where g.user_uuid = $1 and g.completed = false and gc.category_id = $2 order by g.due_date asc";
    $p = array($uuid, (int)$cid);
} else {
    $sql = "select * from goals where user_uuid = $1 and completed = false order by due_date asc";
    $p = array($uuid);
}


$res = pg_query_params($db, $sql, $p);
$goals = array();
$ids = array();
while ($row = pg_fetch_assoc($res)) {
    $goals[] = $row;
    $ids[] = $row['id'];
}

$map = fetch_categories_for_goals($db, $ids);
foreach ($goals as &$g) {
    $g['categories'] = $map[$g['id']] ?? array();
}

pg_close($db);

header('Content-Type: application/json');
echo json_encode(array("status" => "success", "goals" => $goals));
?>
