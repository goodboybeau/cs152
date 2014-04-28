import Data.Char
import Data.Int

{--Problem 1--}

{--begin helper functions--}
discriminant :: (Floating a) => a -> a -> a -> a
discriminant a b c = b * b - 4 * a * c

plus_and_minus :: Floating a => a -> a -> (a,a)
plus_and_minus a b = (a+b, a-b)

divide_each :: Floating a => (a,a) -> a -> (a,a)
divide_each (a,b) c = (a/c, b/c)
{--begin helper functions--}

quadratic1 :: (Eq a, Floating a) => a -> a -> a -> a
quadratic1 a b c = 
	if a == 0
	then (-c) / b
	else (-b + sqrt(discriminant a b c)) / (2 * a)

quadratic2 :: (Eq a, Floating a) => a -> a -> a -> (a,a)
quadratic2 a b c = divide_each (sums) (2*a)
	where sums = plus_and_minus (-b) (sqrt(discriminant a b c))

quadratic :: (Ord a, Eq a, Floating a) => a -> a -> a -> Maybe (a,a)
quadratic a b c =
	if (discriminant a b c) >= 0
	then Just (divide_each (plus_and_minus (-b) (sqrt(discriminant a b c))) (2*a))
	else Nothing

{--Problem 2--}
startsWithCapital :: String -> Bool
startsWithCapital string =
	if string == [] then False
	else head string == toUpper (head string)

{--Problem 3--}
flipAll :: [(Int, Int)] -> [(Int,Int)]
flipAll list = 
	if list == [] then []
	else (snd front, fst front) : flipAll(tail list)
		where front = head list

{--Problem 4--}

------------------------------------------------------
------ definitions of and for the OrderedTree type ---
------------------------------------------------------


-- ordered trees may be empty, or consist of an integer root
--   and a list of children
-- children should not be of the Nil subtype
-- the type supports default equality testing

data OrderedTree = Nil | Node Integer [ OrderedTree ]
	deriving (Eq)


-- a simple-minded "show" function for ordered Trees 
--   (does a preorder traversal)

instance Show OrderedTree where
	show Nil = ""
	show (Node value list) = (show value) ++ " " ++ (concat (map show list))

size :: OrderedTree -> Int
size tree = length(words (show tree))

{--Problem 5--}
combinedLengths :: Eq(a) => [[a]] -> Int
combinedLengths lists =
	if lists == [] then 0
	else (length (head(lists))) + combinedLengths(tail(lists))

combinedLengths2 :: Eq(a) => [[a]] -> Int
combinedLengths2 lists = 
	sum (map length lists)

{--Problem 6--}
stripAnyCapitals :: String -> String
stripAnyCapitals string =
	if string == ""
	then ""
	else
		thisCall ++ stripAnyCapitals( tail(string))
		where
			thisCall =
				if not (head string == toUpper (head string))
				then (head string) : ""
				else ""

stripCapitals :: String -> Either String String
stripCapitals string =
	if not (head string == toUpper(head string)) 
		then Left "initial letter isn't upper case"
	else if string == "" then Right ""
	else case stripCapitals(backs) of
		Left x -> Right (backs)
		Right result -> Right result
		where backs = tail string

stripCapitalsSuccess :: (Either String String) -> Bool
stripCapitalsSuccess x =
	not (x == stripCapitals "fail")

isLegalIdentifier :: String -> Bool
isLegalIdentifier string =
	let stripped = stripCapitals string 
	in
	if not (stripCapitalsSuccess stripped)
		then False
	else ( length((either (const []) id stripped)) )== 0

{--Problem 7--}
stripMatchingCharacters :: (Char -> Bool) -> String -> String
stripMatchingCharacters constraint string = 
	dropWhile constraint string

{--Problem 8--}
data Point p = 
	Point { x :: Double
		, y :: Double
		}
	deriving(Eq)
{--
instance (Eq p, Show p) => Show (Point p) where
	show point = "(" ++ x ++ "," ++ y ++ ")"
--}

{--Problem 9--}
applyAll :: [[a] -> [a]] -> [a] -> [a]
applyAll functions values = 
	if length(functions) == 0 then values
	else applyAll ( tail functions ) ( (head functions) values ) 