MESSAGE="$1"
BRANCH="$2"

git add . &&
git commit -m MESSAGE &&
git pull origin BRANCH &&
git push origin BRANCH
