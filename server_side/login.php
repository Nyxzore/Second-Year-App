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

function is_profane($text) {
    // 1. Normalize characters to catch bypass attempts
    $normalized = strtolower(str_replace(
        ['0', '1', '3', '4', '5', '7', '8', '@', '$', '!', '|'],
        ['o', 'i', 'e', 'a', 's', 't', 'b', 'a', 's', 'i', 'l'],
        $text
    ));

    $data = json_encode(array("message" => $normalized));
    $ctx = stream_context_create(array(
        'http' => array(
            'method'  => 'POST',
            'header'  => "Content-Type: application/json\r\n",
            'content' => $data,
            'timeout' => 4
        )
    ));

    $response = @file_get_contents("https://vector.profanity.dev", false, $ctx);

    if ($response === false) {
        // If API fails, fallback to PurgoMalum as a second layer
        $url = "https://www.purgomalum.com/service/containsprofanity?text=" . urlencode($normalized);
        $response = @file_get_contents($url);
        return trim($response) === "true";
    }

    $res_json = json_decode($response, true);

    return (isset($res_json['isProfanity']) && $res_json['isProfanity'] === true) ||
           (isset($res_json['isProfane']) && $res_json['isProfane'] === true) ||
           (isset($res_json['score']) && $res_json['score'] > 0.9);
}

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
    try {
        if (!$user_in || !$hash || !$mail) {
            throw new Exception("All fields are required");
        }

        if (is_profane($user_in)) {
            throw new Exception("Inappropriate username detected");
        }

        $res = pg_query_params($db, "select 1 from accounts where username = $1", array($user_in));
        if ($res && pg_fetch_assoc($res)) {
            throw new Exception("Username already taken");
        }

        $res = pg_query_params($db, "select 1 from accounts where email = $1", array($mail));
        if ($res && pg_fetch_assoc($res)) {
            throw new Exception("Email already registered");
        }

        $sql = "insert into accounts (username, password, email, admin) values ($1, $2, $3, $4) returning userid";
        $res = pg_query_params($db, $sql, array($user_in, $hash, $mail, $adm));

        if (!$res) {
            throw new Exception("Database error: " . pg_last_error($db));
        }

        $row = pg_fetch_assoc($res);
        return array("status" => "success", "uuid" => $row['userid'], "message" => "Account created");

    } catch (Exception $e) {
        return array("status" => "failure", "message" => $e->getMessage());
    }
}

if ($mode === "login") $resp = login();
else if ($mode === "create_account") $resp = create();

pg_close($db);
header('Content-Type: application/json');
echo json_encode($resp);
?>
