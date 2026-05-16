<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";

$uuid = $_POST['uuid'] ?? null;
$fid = $_POST['friend_id'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));

$sql = "update friendships set status = 'active' where user_id1 = $1 and user_id2 = $2 and status = 'pending'";
$res = pg_query_params($db, $sql, array($fid, $uuid));

if ($res && pg_affected_rows($res) > 0) {
    echo json_encode(array("status" => "success"));
} else {
    echo json_encode(array("status" => "failure"));
}

pg_close($db);
?>
