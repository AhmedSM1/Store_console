// MongoDB init script for products collection
// Switch to products database
db = db.getSiblingDB('products_db');

// Initiate replica set if not already initiated
try {
    let status = rs.status();
    if (status.ok && (status.myState === 1 || status.myState === 2)) {
        console.log("Replica set already initiated");
    } else {
        throw new Error("Initiate needed");
    }
} catch (err) {
    console.log("Initiating replica set...");
    try {
        rs.initiate({
            _id: "rs0",
            members: [{ _id: 0, host: "localhost:27017" }]
        });
    } catch (initErr) {
        console.log("Initiation already in progress or failed: " + initErr.message);
    }

    // Wait for primary
    let count = 0;
    while (count < 30) {
        let isMaster = db.isMaster();
        if (isMaster.ismaster) {
            console.log("Became primary in " + count + " seconds");
            break;
        }
        console.log("Waiting for primary... (state: " + isMaster.me + ")");
        sleep(1000);
        count++;
    }
}

// Create products collection and insert sample data
db.products.insertMany([
    // Electronics (8 products)
    {
        name: 'iPhone 18',
        description: 'The all new iPhone 18 from Apple with advanced AI features',
        price: NumberDecimal('5500'),
        quantity: 100,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Samsung Galaxy S26',
        description: 'Latest Samsung flagship with 200MP camera',
        price: NumberDecimal('4800'),
        quantity: 75,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'MacBook Pro 16',
        description: 'Apple MacBook Pro with M4 chip and 32GB RAM',
        price: NumberDecimal('12000'),
        quantity: 30,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Sony WH-1000XM6',
        description: 'Premium noise-cancelling wireless headphones',
        price: NumberDecimal('1500'),
        quantity: 150,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'iPad Pro 13',
        description: 'Apple iPad Pro with M3 chip and OLED display',
        price: NumberDecimal('4500'),
        quantity: 60,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'LG OLED TV 65"',
        description: '65 inch 4K OLED Smart TV with Dolby Vision',
        price: NumberDecimal('8500'),
        quantity: 25,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'PlayStation 6',
        description: 'Next-gen gaming console with 8K support',
        price: NumberDecimal('2500'),
        quantity: 40,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Apple Watch Ultra 3',
        description: 'Premium smartwatch for outdoor adventures',
        price: NumberDecimal('3200'),
        quantity: 80,
        Category: 'electronics',
        _class: 'sa.com.store.products.entity.Product'
    },

    // Furniture (8 products)
    {
        name: 'Modern Leather Sofa',
        description: 'Italian leather 3-seater sofa in black',
        price: NumberDecimal('4500'),
        quantity: 15,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Oak Dining Table',
        description: 'Solid oak dining table seats 8 people',
        price: NumberDecimal('3200'),
        quantity: 20,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'King Size Bed Frame',
        description: 'Walnut wood king size bed frame with headboard',
        price: NumberDecimal('2800'),
        quantity: 12,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Executive Office Chair',
        description: 'Ergonomic leather office chair with lumbar support',
        price: NumberDecimal('1200'),
        quantity: 50,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Bookshelf Unit',
        description: '5-tier wooden bookshelf in walnut finish',
        price: NumberDecimal('850'),
        quantity: 35,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Coffee Table',
        description: 'Modern glass top coffee table with metal base',
        price: NumberDecimal('650'),
        quantity: 40,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Wardrobe Cabinet',
        description: 'Large 4-door wardrobe with mirror',
        price: NumberDecimal('2200'),
        quantity: 18,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'TV Stand Entertainment Unit',
        description: 'Modern TV stand with storage compartments',
        price: NumberDecimal('980'),
        quantity: 28,
        Category: 'furniture',
        _class: 'sa.com.store.products.entity.Product'
    },

    // Groceries (7 products)
    {
        name: 'Organic Olive Oil 1L',
        description: 'Extra virgin organic olive oil from Italy',
        price: NumberDecimal('85'),
        quantity: 200,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Basmati Rice 5kg',
        description: 'Premium aged basmati rice from India',
        price: NumberDecimal('45'),
        quantity: 300,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Arabic Coffee Beans 1kg',
        description: 'Premium roasted Arabic coffee beans',
        price: NumberDecimal('120'),
        quantity: 150,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Honey Natural 500g',
        description: 'Pure natural honey from local farms',
        price: NumberDecimal('65'),
        quantity: 180,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Mixed Nuts 1kg',
        description: 'Premium mixed nuts (almonds, cashews, pistachios)',
        price: NumberDecimal('95'),
        quantity: 120,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Tahini Paste 400g',
        description: 'Organic sesame tahini paste',
        price: NumberDecimal('35'),
        quantity: 200,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Saffron 10g',
        description: 'Premium Iranian saffron threads',
        price: NumberDecimal('180'),
        quantity: 50,
        Category: 'groceries',
        _class: 'sa.com.store.products.entity.Product'
    },

    // Clothes (7 products)
    {
        name: 'Men\'s Business Suit',
        description: 'Italian wool business suit in navy blue',
        price: NumberDecimal('2500'),
        quantity: 25,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Women\'s Evening Dress',
        description: 'Elegant silk evening dress in black',
        price: NumberDecimal('1800'),
        quantity: 30,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Premium Cotton T-Shirt',
        description: 'Soft organic cotton t-shirt, multiple colors',
        price: NumberDecimal('150'),
        quantity: 200,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Leather Jacket',
        description: 'Genuine leather biker jacket for men',
        price: NumberDecimal('1200'),
        quantity: 35,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Designer Jeans',
        description: 'Premium denim jeans with perfect fit',
        price: NumberDecimal('450'),
        quantity: 80,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Cashmere Sweater',
        description: 'Luxurious cashmere pullover sweater',
        price: NumberDecimal('800'),
        quantity: 45,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    },
    {
        name: 'Sports Running Shoes',
        description: 'Professional running shoes with cushioning',
        price: NumberDecimal('650'),
        quantity: 100,
        Category: 'clothes',
        _class: 'sa.com.store.products.entity.Product'
    }
]);

print('Successfully inserted 30 products into products_db.products collection');
