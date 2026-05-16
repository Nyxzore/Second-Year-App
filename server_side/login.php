<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$pass = "c434b13a28cd859c169a";

$user_in = $_POST['username'] ?? null;
$hash = $_POST['hash'] ?? null;
$mode = $_POST['mode'] ?? "login";
$mail = $_POST['email'] ?? null;
$adm = $_POST['is_admin'] ?? "0";

$db = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$pass");
if (!$db) exit(json_encode(array("status" => "error")));
    
function login() {
    global $db, $user_in, $hash;
    if (!$user_in || !$hash) return array("status" => "failure", "message" => "empty fields");
    
    $sql = "select password, userid, admin, profile_picture from accounts where username = $1";
    $res = pg_query_params($db, $sql, array($user_in));
    
    if ($row = pg_fetch_assoc($res)) {
        if (hash_equals($row['password'], $hash)) {
            return array(
                "status" => "success",
                "uuid" => $row['userid'],
                "is_admin" => $row['admin'],
                "profile_pic" => $row['profile_picture']
            );
        }
    }
    return array("status" => "failure", "message" => "invalid");
}

function create() {
    global $db, $user_in, $hash, $mail, $adm;
    if (!$user_in || !$hash) return array("status" => "failure", "message" => "empty fields");

    $res = pg_query_params($db, "select username from accounts where username = $1", array($user_in));
    if (pg_fetch_assoc($res)) return array("status" => "failure", "message" => "taken");

    $sql = "insert into accounts (username, password, email, admin) values ($1, $2, $3, $4) returning userid";
    $res = pg_query_params($db, $sql, array($user_in, $hash, $mail, $adm));
    
    if ($res) {
        $row = pg_fetch_assoc($res);
        return array("status" => "success", "uuid" => $row['userid']);
    }
    return array("status" => "error");
}

if ($mode === "login") $resp = login();
else if ($mode === "create_account") $resp = create();

pg_close($db);
header('Content-Type: application/json');
echo json_encode($resp);
?>
