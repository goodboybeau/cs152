import Char

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



{--Part 1--}

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
  if a == 0 then Nothing
  else if (discriminant a b c) >= 0
  then Just (divide_each (plus_and_minus (-b) (sqrt(discriminant a b c))) (2*a))
  else Nothing

{--Part 2--}
startsWithCapital :: String -> Bool
startsWithCapital string =
  if string == [] then False
  else head string == toUpper (head string)

{--Part 3--}
flipall :: [(Int, Int)] -> [(Int,Int)]
flipall list = 
  if list == [] then []
  else (snd front, fst front) : flipall(tail list)
    where front = head list

{--Part 4--}
size :: OrderedTree -> Int
size tree = length(words (show tree))

{--Part 5--}
combinedLengths :: [[a]] -> Int
combinedLengths [] = 0
combinedLengths lists =  
  length (head(lists)) + combinedLengths(tail(lists))

combinedLengths2 :: [[a]] -> Int
combinedLengths2 lists = 
  sum (map length lists)

{--Part 6--}
stripAnyCapitals :: String -> String
stripAnyCapitals "" = ""
stripAnyCapitals string = dropWhile isUpper string

stripCapitals :: String -> Either String String
stripCapitals "" = Right ""
stripCapitals string =
  if not (head string == toUpper(head string)) 
    then Left "initial letter isn't upper case"
  else if string == "" then Right ""
  else Right (stripAnyCapitals string)

stripCapitalsSuccess :: (Either String String) -> Bool
stripCapitalsSuccess x =
  not (x == stripCapitals "fail")

isLegalIdentifier :: String -> Bool
isLegalIdentifier string =
  process ( stripCapitals string )
  where 
    process (Left nope) = False
    process (Right stripped) = length stripped == 1 && isDigit( head stripped )

{--Part 7--}
stripMatchingCharacters :: (Char -> Bool) -> String -> String
stripMatchingCharacters constraint string = 
  dropWhile constraint string

{--Part 8--}
data Point = 
      Point { x, y :: Double }
  deriving(Eq)

instance Show Point where
  show (Point x y) = "(" ++ show x ++ "," ++ show y ++ ")"

xCoord :: Point -> Double
xCoord (Point x _) = x

yCoord :: Point -> Double
yCoord (Point _ y) = y

distance :: Point -> Point -> Double
distance p q = sqrt((  (((xCoord p) - (xCoord q))^2) + (((yCoord p) - (yCoord q))^2)))


{--Part 9--}
applyAll :: [[a] -> [a]] -> [a] -> [a]
applyAll functions values = 
  if length(functions) == 0 then values
  else applyAll ( tail functions ) ( (head functions) values ) 


{--Part 10--}

tailHelper x list
  | head ( reverse list ) == x = tail ( reverse list )
  | otherwise = head( reverse list ): removeFirstOccurrence x (tail (reverse list))

removeFirstOccurrence x ls 
  | null ls = []   
  | x == head ls = tail ls
  | otherwise = (head ls):(tailHelper x (tail ls))


-- Tests the parse function.


-- test1 :: [(Floating, Floating)]
test1a =   
   [ 
     (quadratic1 1 (-3) 2),
     (quadratic1 1  3 2),
     (quadratic1 1 (-1) (-1)),
     (quadratic1 6 (-5) 1)
   ]

test1b =   
   [ 
     (quadratic2 1 (-3) 2),
     (quadratic2 1  3 2),
     (quadratic2 1 (-1) (-1)),
     (quadratic2 6 (-5) 1)
   ]

test1c =   
   [ 
     (quadratic 1 (-3) 2),
     (quadratic 1  3 2),
     (quadratic 1 (-1) (-1)),
     (quadratic 6 (-5) 1),
     (quadratic 0 1 (-3)),
     (quadratic 1 1 1)
   ]

test2 = 
   [
     (startsWithCapital ""),
     (startsWithCapital "CS 152"),
     (startsWithCapital "152"),
     (startsWithCapital "hello!")
   ]

test3 = 
   [ (flipall []),
     (flipall [(17,76)]),
     (flipall [(1,1), (2,4), (3,9)])
   ]

test4 = 
   let tree = Node 1 [(Node 11 []),
                     (Node 12 [(Node 121 [])]),
                     (Node 13 [(Node 131 []), (Node 132 [])])
                    ]
   in 
      [ (size tree),
        (size (Node 0 [tree, tree])),
        (size (Node 0 [tree, (Node 01 [tree]), (Node 02 [tree])])) 
      ]

test5a =
   [
      (combinedLengths []),
      (combinedLengths [[1]]),
      (combinedLengths [[1], [2, 3], []]),
      (combinedLengths [[1], [2, 3], [4,5,6]]),
      (combinedLengths [[1], [2, 3], [4,5,6], [1], [2, 3], [4,5,6]])
   ]
    
test5b =
   [
      (combinedLengths2 []),
      (combinedLengths2 [[1]]),
      (combinedLengths2 [[1], [2, 3], []]),
      (combinedLengths2 [[1], [2, 3], [4,5,6]]),
      (combinedLengths2 [[1], [2, 3], [4,5,6], [1], [2, 3], [4,5,6]])
   ]

test6a = 
   [  (stripAnyCapitals ""),
      (stripAnyCapitals "hello"),
      (stripAnyCapitals "HELLO"),
      (stripAnyCapitals "HELLO!"),
      (stripAnyCapitals "CS 152"),
      (stripAnyCapitals "Computer Science")
   ]

test6b = 
   [  (stripCapitals ""),
      (stripCapitals "hello"),
      (stripCapitals "HELLO"),
      (stripCapitals "HELLO!"),
      (stripCapitals "CS 152"),
      (stripCapitals "Computer Science")
   ]

test6c = 
   [  (isLegalIdentifier "ABC3"),
      (isLegalIdentifier "ABC34"),
      (isLegalIdentifier "ABC3qr"),
      (isLegalIdentifier "ABC"),
      (isLegalIdentifier "NeedMoreCases!!??")
   ]

test7 = 
   [
      (stripMatchingCharacters isUpper "ABC3qr"),
      (stripMatchingCharacters isLower "ABC3qr"),
      (stripMatchingCharacters isSpace " ABC3qr"),
      ((stripMatchingCharacters isSpace) "ABC3qr"),
      (stripMatchingCharacters (\ x -> x >= 'A' && x < 'C') "ABC3qr")
   ]

test8a =
   [
     ((Point 3 0) == (Point 0 (-4))),
     ((Point 1 2) == (Point (-3) 2)),
     ((Point 1.2 3.4) == (Point 1.2 3.4)),
     ((Point 3 0) /= (Point 0 (-4))),
     ((Point 1 2) /= (Point (-3) 2)),
     ((Point 1.2 3.4) /= (Point 1.2 3.4))
   ]

test8b =
   [
     (distance (Point 3 0) (Point 0 (-4))),
     (distance (Point 1 2) (Point (-3) 2)),
     (distance (Point 1.2 3.4) (Point 1.2 3.4)),
     (distance (Point 10 20) (Point 20 30))
   ]

test9 = 
   [
     (applyAll [tail, tail, tail, tail] [1,2,3,4,5]),
     (applyAll [init, init, init, init] [1,2,3,4,5]),
     (applyAll [(6 :), (7 :), (8 :)] [1,2,3,4,5]),
     (applyAll [reverse, reverse, reverse] [1,2,3,4,5]),
     (applyAll [(map (* 2)), (map (+ 1))] [1,2,3,4,5]),
     (applyAll [(\x -> [maximum x, minimum x])] [1,2,3,4,5]),
     (applyAll [] [1,2,3,4,5])
   ]

test10 = 
  [
     (removeFirstOccurrence 4 [1,4,2,3,4,4]),
     (removeFirstOccurrence 2 [1,4,2,3,4,4]),
     (removeFirstOccurrence 2 [2,3,4,4]),
     (removeFirstOccurrence 2 [3,4,4,2]),
     (removeFirstOccurrence 12 [2,3,4,4])
  ]

main = do 
  putStrLn (show test1a)
  putStrLn ""
  putStrLn (show test1b)
  putStrLn ""
  putStrLn (show test1c)
  putStrLn ""
  putStrLn (show test2)
  putStrLn ""
  putStrLn (show test3)
  putStrLn ""
  putStrLn (show test4)
  putStrLn ""
  putStrLn (show test5a)
  putStrLn ""
  putStrLn (show test5b)
  putStrLn ""
  putStrLn (show test6a)
  putStrLn ""
  putStrLn (show test6b)
  putStrLn ""
  putStrLn (show test6c)
  putStrLn ""
  putStrLn (show test7)
  putStrLn ""
  putStrLn (show (Point (-4) 5.5))
  putStrLn ""
  putStrLn (show test8a)
  putStrLn ""
  putStrLn (show test8b)
  putStrLn ""
  putStrLn (show test9)
  putStrLn ""
  putStrLn (show test10)
  putStrLn ""

