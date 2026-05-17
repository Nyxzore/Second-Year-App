<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));

$sql = "SELECT userid, username, profile_picture FROM accounts WHERE userid IN (
    SELECT user_id2 FROM friendships WHERE user_id1 = $1 AND status = 'active'
    UNION
    SELECT user_id1 FROM friendships WHERE user_id2 = $1 AND status = 'active'
) ORDER BY username ASC";
$res = pg_query_params($db, $sql, array($uuid));

$friends = array();
while ($row = pg_fetch_assoc($res)) {
    $friends[] = array(
        "id" => $row['userid'],
        "username" => $row['username'],
        "profile_pic" => (int)($row['profile_picture'] ?? 0),
        "status" => "accepted"
    );
}

pg_close($db);
header('Content-Type: application/json');
echo json_encode(array("status" => "success", "friends" => $friends));
?>
