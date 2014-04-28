{-------------------------------------------------

 This binary search tree is the same as the
   improved version, except that 
   (1)  it provides explicit names for the components, 
        and thus allows them to be selected by
        accessor functions of these names
   (2)  it provides a more useful definition of show,
        which doesn't call flatten

-------------------------------------------------}

data NewBST a =
  Nil  | Node { val :: a
         , left :: (NewBST a)
         , right :: (NewBST a)
         } 
  deriving (Eq)

 
instance (Eq a, Show a) => Show (NewBST a) where
  show bst = showrec bst 0
    where showrec Nil _ = ""
          showrec b n = showrec (left b) (n + 2) ++
                        replicate n ' ' ++
                        show (val b) ++
                        "\n" ++
                        showrec (right b) (n + 2)
     

{------------------------------------------------- 

   These functions and data values work as 
     in the original BST definition, but explicit
     selectors are used rather than pattern
     matching.

-------------------------------------------------}

-- puts duplicate insertions into right subtree

-- insert :: Ord a => a -> NewBST a -> NewBST a
-- insert x Nil = (NonemptyBST x Nil Nil)

insert :: Ord a => a -> NewBST a -> NewBST a
insert x bst =
  if bst == Nil 
  then (Node x Nil Nil)
  else if x < (val bst)
  then Node (val bst) (insert x (left bst)) (right bst)
  else Node (val bst) (left bst) (insert x (right bst))


reflect :: Eq a => NewBST a -> NewBST a
reflect bst = 
  if bst == Nil
  then Nil
  else Node (val bst) (reflect (right bst)) (reflect (left bst))      

 
flatten :: NewBST a -> [a]
flatten Nil = []
flatten (Node val left right) = 
  (flatten left)++[val]++
         (flatten right)

    ------  some sample data values -------

sampleTree :: NewBST Int
sampleTree = 
  (Node 4 
    (Node 2 Nil Nil)
    (Node 7 (Node 6 Nil Nil) Nil))

newTree :: NewBST Int
newTree = 
  (insert 6 (insert 7 (insert 2 (insert 4 Nil))))

equivalentTree :: NewBST Int
equivalentTree = 
  (insert 4 (insert 7 (insert 2 (insert 6 Nil))))


main = do
  print sampleTree
  print (reflect sampleTree)
  print (flatten sampleTree)
  print (flatten (reflect sampleTree))
  print (newTree == sampleTree)
  print (equivalentTree == sampleTree)