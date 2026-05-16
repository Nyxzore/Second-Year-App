<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$friend_username = $_POST['friend_username'] ?? null;

$dbconn = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$password_db");

if (!$dbconn) {
    echo "Database connection failed";
    exit;
}

$res = pg_query_params($dbconn, "SELECT userid FROM accounts WHERE username = $1", array($friend_username));
$row = pg_fetch_assoc($res);

if (!$row) {
    echo "User not found";
    exit;
}

$friend_id = $row['userid'];

if ($uuid == $friend_id) {
    echo "You cannot add yourself";
    exit;
}

$SQL = "INSERT INTO friendships (user_id1, user_id2, status) VALUES ($1, $2, 'pending')";
$result = pg_query_params($dbconn, $SQL, array($uuid, $friend_id));

if ($result) {
    echo "Request sent to " . $friend_username;
} else {
    echo "Already friends or request exists";
}
pg_close($dbconn);
?>