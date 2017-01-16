INSERT INTO _FactPriceTemp  
	(Price, 
	ProductID, 
	ShippersID,
	ShipCountry, 
	ShipCompany,
	Category,
	Suppliers,
	Country)
SELECT
	(OrderDetails.UnitPrice * OrderDetails.Quantity),
	0,
	0,
	_ShipTemp.ShipCountry,
	_ShipTemp.CompanyName,
	_CategTemp.CategoryName,
	_SupplTemp.CompanyName,
	_SupplTemp.Country
FROM 
	OrderDetails,
	
	(SELECT Orders.OrderID, Shippers.CompanyName, Orders.ShipCountry
	FROM Orders, Shippers
	WHERE Orders.ShipVia=Shippers.ShipperID) _ShipTemp,
	
	(SELECT Products.ProductID, Categories.CategoryName
	FROM Products, Categories
	WHERE Products.CategoryID=Categories.CategoryID) _CategTemp,
	
	(SELECT Products.ProductID, Suppliers.CompanyName, Suppliers.Country
	FROM Products, Suppliers
	WHERE Products.SupplierID=Suppliers.SupplierID) _SupplTemp
	
WHERE OrderDetails.OrderID = _ShipTemp.OrderID
	AND OrderDetails.ProductID = _CategTemp.ProductID
	AND OrderDetails.ProductID = _SupplTemp.ProductID



	
INSERT INTO _DimProducts 
	(Category, 
	Supplires, 
	Country) 
SELECT DISTINCT 
	_Categ.CategoryName, 
	_Suppl.CompanyName, 
	_Suppl.Country 	
FROM Products 
LEFT JOIN (SELECT CategoryID, CategoryName FROM Categories)
	AS _Categ ON Products.CategoryID=_Categ.CategoryID
LEFT JOIN (SELECT SupplierID, CompanyName, Country FROM Suppliers)
	AS _Suppl ON Products.SupplierID=_Suppl.SupplierID

	


INSERT INTO _FactPrice
	(Price,
	ProductID,
	ShippersID)
SELECT 
	SUM(_FactPriceTemp.Price) AS sumPrice,
	_DimProducts.ProductID,
	_DimShippers.ShippersID
FROM _FactPriceTemp, _DimProducts, _DimShippers
WHERE _FactPriceTemp.Category = _DimProducts.Category
	AND _FactPriceTemp.Suppliers = _DimProducts.Suppliers
	AND _FactPriceTemp.Country = _DimProducts.Country
	AND _FactPriceTemp.ShipCountry = _DimShippers.ShipCountry
	AND _FactPriceTemp.ShipCompany = _DimShippers.ShipCompany
GROUP BY _DimProducts.ProductID, _DimShippers.ShippersID




SELECT 
	_FactPrice.Price, 
	_FactPrice.ProductID, 
	_FactPrice.ShippersID
FROM 
	_FactPrice, 
	_DimProducts, 
	_DimShippers
WHERE 
	_FactPrice.ProductID = _DimProducts.ProductID
	AND _FactPrice.ShippersID = _DimShippers.ShippersID
	AND _DimProducts.Suppliers="Leka Trading"

	


UPDATE _FactPrice
SET _FactPrice.*ProductID* = *1*
WHERE 
_FactPrice.Price IS NOT NULL
AND *_DimProducts*.*Suppliers*="value"


106 + 141 = 247











