<?php
header('Content-Type: application/json');
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;

$dbconn = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$password_db");

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$SQL = "SELECT a.userid, a.username
        FROM accounts a
        JOIN friendships f ON f.user_id1 = a.userid
        WHERE f.user_id2 = $1
          AND f.status = 'pending'
        ";

$result = pg_query_params($dbconn, $SQL, array($uuid));

$requests = [];
if ($result) {
    while ($row = pg_fetch_assoc($result)) {
        $requests[] = ["id" => $row['userid'], "username" => $row['username']];
    }
}

echo json_encode(["status" => "success", "requests" => $requests]);
pg_close($dbconn);
?>