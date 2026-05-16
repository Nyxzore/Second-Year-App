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

if (!$db) exit(json_encode(array("status" => "error", "message" => "db fail")));
if (!$uuid) exit(json_encode(array("status" => "error", "message" => "no uuid")));

if ($cid) {
    $sql = "select h.*, exists (select 1 from habit_completions hc where hc.habit_id = h.id and hc.completion_date = current_date) as completed_today from habits h join habit_categories hcat on h.id = hcat.habit_id where h.user_uuid = $1 and hcat.category_id = $2 order by h.name asc";
    $p = array($uuid, (int)$cid);
} else {
    $sql = "select h.*, exists (select 1 from habit_completions hc where hc.habit_id = h.id and hc.completion_date = current_date) as completed_today from habits h where h.user_uuid = $1 order by h.name asc";
    $p = array($uuid);
}

$res = pg_query_params($db, $sql, $p);
$habits = array();
$ids = array();
while ($row = pg_fetch_assoc($res)) {
    $row['completed_today'] = ($row['completed_today'] === 't');
    $habits[] = $row;
    $ids[] = $row['id'];
}

$map = fetch_categories_for_habits($db, $ids);
foreach ($habits as &$h) {
    $h['categories'] = $map[$h['id']] ?? array();
}

pg_close($db);

header('Content-Type: application/json');
echo json_encode(array("status" => "success", "habits" => $habits));
?>
