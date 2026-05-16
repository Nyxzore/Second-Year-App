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
if (!$db) exit(json_encode(array("status" => "error", "message" => "Database connection failed")));
    
function login() {
    global $db, $user_in, $hash;
    if (!$user_in || !$hash) return array("status" => "failure", "message" => "empty fields");
    
    $sql = "select password, userid, admin, profile_picture from accounts where username = $1";
    $res = pg_query_params($db, $sql, array($user_in));
    
    if ($res && $row = pg_fetch_assoc($res)) {
        if (hash_equals($row['password'], $hash)) {
            return array(
                "status" => "success",
                "uuid" => $row['userid'],
                "is_admin" => $row['admin'],
                "profile_pic" => $row['profile_picture'],
                "message" => "Login successful"
            );
        }
    }
    return array("status" => "failure", "message" => "invalid credentials");
}

function create() {
    global $db, $user_in, $hash, $mail, $adm;
    if (!$user_in || !$hash || !$mail) return array("status" => "failure", "message" => "empty fields");

    $res = pg_query_params($db, "select 1 from accounts where username = $1", array($user_in));
    if ($res && pg_fetch_assoc($res)) return array("status" => "failure", "message" => "Username already taken");

    $res = pg_query_params($db, "select 1 from accounts where email = $1", array($mail));
    if ($res && pg_fetch_assoc($res)) return array("status" => "failure", "message" => "Email already registered");

    $sql = "insert into accounts (username, password, email, admin) values ($1, $2, $3, $4) returning userid";
    $res = pg_query_params($db, $sql, array($user_in, $hash, $mail, $adm));
    
    if ($res) {
        $row = pg_fetch_assoc($res);
        return array("status" => "success", "uuid" => $row['userid'], "message" => "Account created");
    }
    return array("status" => "error", "message" => "Database error: " . pg_last_error($db));
}

if ($mode === "login") $resp = login();
else if ($mode === "create_account") $resp = create();

pg_close($db);
header('Content-Type: application/json');
echo json_encode($resp);
?>
