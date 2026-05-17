<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";

$mode = $_POST['mode'] ?? '';
$uuid = $_POST['uuid'] ?? '';
$pic = $_POST['profile_pic'] ?? '';

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));
    
if ($mode === "update_profile_pic") {
    $sql = "update accounts set profile_picture = $1 where userid = $2";
    $res = pg_query_params($db, $sql, array($pic, $uuid));
    if ($res) $resp = array("status" => "success");
    else $resp = array("status" => "error");
} else {
    $resp = array("status" => "error", "message" => "bad mode");
}

pg_close($db);
header('Content-Type: application/json');
echo json_encode($resp);
?>

